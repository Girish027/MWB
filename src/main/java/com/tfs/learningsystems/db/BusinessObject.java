package com.tfs.learningsystems.db;

import static org.springframework.data.jpa.domain.Specification.where;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tfs.learningsystems.auth.ActionContext;
import com.tfs.learningsystems.exceptions.ApplicationException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.tfs.learningsystems.util.Constants;
import com.tfs.learningsystems.util.ErrorMessage;
import lombok.Data;
import com.tfs.learningsystems.ui.model.Error;
import com.tfs.learningsystems.ui.model.error.AlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import javax.ws.rs.core.Response;

/**
 * The base class of all business objects. This class defines the workflow of certain operations,
 * e.g. create, update, ... This will help to unify and enforce requirements like security and data
 * segmentations. Each subclass may override sub-steps in these operations to achieve individual
 * needs.
 */
@Slf4j
@Data
public abstract class BusinessObject<T extends BusinessObject, S extends Serializable> {

  public static final String OBJ_ID = "id";
  public static final String DB_ID = "dbid";
  public static final String CLIENT_CID = "cid";
  public static final String MODIFIED_AT = "modifiedAt";
  public static final String MODIFIED_BY = "modifiedBy";

  //
  // legacy of having different names
  //
  public static final String UPDATED_AT = "updatedAt";
  public static final String CREATED_AT = "createdAt";
  public static final String CREATED_BY = "createdBy";
  //
  // sanitizing fields
  //
  protected static final Logger LOGGER = LoggerFactory.getLogger(BusinessObject.class);

  public static void sanitize(Object obj) {
    PolicyFactory sanitizer = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.STYLES)
            .and(Sanitizers.TABLES);
    try {
      for (Field fld : obj.getClass().getDeclaredFields()) {
        // all our fields are defined as 'protected', such as ones in ProjectBO
        if (!Modifier.isProtected(fld.getModifiers()) ||
                Modifier.isStatic(fld.getModifiers()) ||
                Modifier.isFinal(fld.getModifiers()) ||
                !fld.getType().isAssignableFrom(String.class) ||
                (!Constants.NAME.equalsIgnoreCase(fld.getName()) &&
                        !Constants.DESCRIPTION.equalsIgnoreCase(fld.getName()))) {
          continue;
        }
        String val = (String) fld.get(obj);
        if (StringUtils.isEmpty(val)) {
          continue;
        }
        String cleanVal = sanitizer.sanitize(val);
        if (!val.equals(cleanVal)) {
          LOGGER
                  .info("Sanitize {} - {} - '{}' - '{}'", obj.getClass().getSimpleName(), fld.getName(),
                          val, cleanVal);
          fld.set(obj, cleanVal);
        }
      }
    } catch (IllegalAccessException e) {
      LOGGER.debug("Failed to access field, while sanitizing ", e);
    }
  }

  public abstract String getDbPrefix();

  public boolean hasId() {
    return (true);
  }

  public boolean hasClientId() {
    return (true);
  }

  public boolean hasDbId() {
    return Arrays.stream(this.getClass().getFields())
            .anyMatch(f -> f.getName().equals(DB_ID));

  }

  @JsonIgnore
  public Field getDbIdField() {
    if (!hasDbId()) {
      return (null);
    }
    try {
      Field dbIdField = this.getClass().getDeclaredField(DB_ID);
      return (dbIdField);

    } catch (Exception e) {
      LOGGER.error("Failed to get DbId field for class : " + this.getClass().getName(), e);
    }
    return (null);
  }

  @JsonIgnore
  public Object getObjId() {
    try {
      Field objIdField = this.getClass().getDeclaredField(OBJ_ID);
      return (objIdField.get(this));

    } catch (Exception e) {
      LOGGER.error("Failed to get obj Id field for class : " + this.getClass().getName(), e);
    }
    return (null);
  }

  @JsonIgnore
  protected String getDbIdValue() {
    try {
      Field dbIdField = getDbIdField();
      if (dbIdField == null) {
        return (null);
      }
      return ((String) dbIdField.get(this));

    } catch (Exception e) {
      LOGGER.error("Failed to get obj Id field for class : " + this.getClass().getName(), e);
    }
    return (null);
  }

  public Specification<T> byClientId(String clientId) {
    return (root, query, cb) -> cb.equal(root.get(CLIENT_CID), clientId);
  }

  public Specification<T> byDbId(String id) {
    return (root, query, cb) -> cb.equal(root.get(DB_ID), id);
  }

  public Specification<T> byId(S id) {
    return (root, query, cb) -> cb.equal(root.get(OBJ_ID), id);
  }

  /**
   * Query database by Id.
   *
   * @param id the Id of the target instance
   * @return the target instance, or null
   */
  public T findOne(S id) {
    if (id == null) {
      return (null);
    }
    String idString = id.toString();
    String escapedStr = StringEscapeUtils.escapeSql(idString);
    if (!idString.equals(escapedStr)) {
      //
      // NT-1158, 'id' shouldn't have any special character
      //
      return (null);
    }
    Specification<T> whereClause;
    if (DbId.isValid(idString)) {
      whereClause = where(byDbId(idString));
    } else {
      whereClause = where(byId(id));
    }
    return (findOne(whereClause, null));
  }

  public T findOne(Map<String, Object> conditions) {
    return (findOne(conditions, null));
  }

  public T findOne(Map<String, Object> conditions, Sort sort) {
    Specification<T> whereClause = null;
    for (Map.Entry<String, Object> one : conditions.entrySet()) {
      Object val = one.getValue();
      if (String.class.isAssignableFrom(val.getClass())) {
        val = StringEscapeUtils.escapeSql(val.toString());
      }
      final Object param = val;
      Specification<T> oneClause = (root, query, cb) -> cb.equal(root.get(one.getKey()), param);
      if (whereClause == null) {
        whereClause = where(oneClause);
      } else {
        whereClause = (whereClause).and(oneClause);
      }
    }
    return (findOne(whereClause, sort));
  }

  public T findOne(String fldName, Object val) {
    if (String.class.isAssignableFrom(val.getClass())) {
      val = StringEscapeUtils.escapeSql(val.toString());
    }
    final Object param = val;
    Specification<T> oneClause = (root, query, cb) -> cb.equal(root.get(fldName), param);
    Specification<T> whereClause = where(oneClause);
    return (findOne(whereClause, null));
  }

  private T findOne(Specification<T> whereClause, Sort sort) {

    List<T> records = list(whereClause, sort);

    if (records == null || records.isEmpty()) {
      return (null);
    }
    return (records.get(0));
  }

  //
  // have a central place to inject clause on 'client_id' for client isolation
  //

  // NT-3024

  private Specification<T> injectClientId(Specification<T> whereClause) {
    if (this.hasClientId()) {

      try {
        //
        // need to inject this Id into the query clause
        //

        this.getClass().getDeclaredField(CLIENT_CID);
        String clientId = ActionContext.getClientId();
        if (!StringUtils.isEmpty(clientId)) {
          (whereClause).and(byClientId(clientId));
        }

      } catch (NoSuchFieldException e) {
        return (whereClause);
      } catch (Exception e) {
        LOGGER.error("Exception while appending cid clause for client id : ", e);
      }
    }
    return (whereClause);
  }

  public List<T> list(Map<String, Object> conditions, Sort sort) {
    Specification<T> whereClause = where(
            (root, query, cb) -> cb.equal(cb.literal(1), cb.literal(1)));
    for (Map.Entry<String, Object> one : conditions.entrySet()) {
      Object val = one.getValue();
      if (String.class.isAssignableFrom(val.getClass())) {
        val = StringEscapeUtils.escapeSql(val.toString());
      }
      final Object param = val;
      Specification<T> oneClause = (root, query, cb) -> cb.equal(root.get(one.getKey()), param);
      if (whereClause == null) {
        whereClause = where(oneClause);
      } else {
        whereClause = (whereClause).and(oneClause);
      }
    }
    return (list(whereClause, sort));
  }

  private List<T> list(Specification<T> whereClause, Sort sort) {
    if (sort == null && hasId()) {
      sort = Sort.by(Sort.Direction.ASC, OBJ_ID);
    } else if (sort == null && !hasId()){
      sort = Sort.by(Sort.Direction.ASC, CLIENT_CID);
    }
    whereClause = injectClientId(whereClause);
    List<T> records = RepositoryUtil.getSpecExecutor(this.getClass()).findAll(whereClause, sort);
    return (records);
  }

  /**
   * Provide paging capability to iterate through records
   *
   * @param conditions a map of conditions that will be combined with 'AND' logic
   * @param start the starting position of the query
   * @param pageSize the size of returned records
   * @param sort the order of returned records. By default is based on the Id
   * @return a list satisfy above
   */
  public List<T> page(Map<String, Object> conditions, int start, int pageSize, Sort sort) {
    Specification<T> whereClause = where(
            (root, query, cb) -> cb.equal(cb.literal(1), cb.literal(1)));
    for (Map.Entry<String, Object> one : conditions.entrySet()) {
      Object val = one.getValue();
      if (String.class.isAssignableFrom(val.getClass())) {
        val = StringEscapeUtils.escapeSql(val.toString());
      }
      final Object param = val;
      Specification<T> oneClause = (root, query, cb) -> cb.equal(root.get(one.getKey()), param);
      if (whereClause == null) {
        whereClause = where(oneClause);
      } else {
        whereClause = (whereClause).and(oneClause);
      }
    }
    return (page(whereClause, start, pageSize, sort));
  }

  private List<T> page(Specification<T> whereClause, int start, int pageSize, Sort sort) {
    if (sort == null && hasId()) {
      sort = Sort.by(Sort.Direction.ASC, OBJ_ID);
    }
    whereClause = injectClientId(whereClause);

    Page<T> onePage = RepositoryUtil.getSpecExecutor(this.getClass())
            .findAll(whereClause, PageRequest.of(start, pageSize, sort));
    List<T> records = onePage.getContent();
    return (records);
  }

  /**
   * Counting the number of records that satisfied the condition
   */
  public long count(Map<String, Object> conditions) {
    Specification<T> whereClause = where(
            (root, query, cb) -> cb.equal(cb.literal(1), cb.literal(1)));
    for (Map.Entry<String, Object> one : conditions.entrySet()) {
      Object val = one.getValue();
      if (String.class.isAssignableFrom(val.getClass())) {
        val = StringEscapeUtils.escapeSql(val.toString());
      }
      final Object param = val;
      Specification<T> oneClause = (root, query, cb) -> cb.equal(root.get(one.getKey()), param);
      if (whereClause == null) {
        whereClause = where(oneClause);
      } else {
        whereClause = (whereClause).and(oneClause);
      }
    }
    whereClause = injectClientId(whereClause);

    return (RepositoryUtil.getSpecExecutor(this.getClass()).count(whereClause));
  }

  /**
   * This will populate common fields, and create an new instance
   *
   * @return the newly created instance
   */
  public T create() {
    preCreate();
    if (!validate()) {
      return (null);
    }
    T obj = null;
    try {
      obj = realCreate();
      postCreate();

      return (obj);
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyExistsException(new Error(Response.Status.CONFLICT.getStatusCode(),
              "already_exists",
              ErrorMessage.CREATE_CONSTRAINT_VIOLATION));
    } catch (Exception e) {
      String msg = e.getMessage();
      Throwable causedBy = e.getCause();
      while (causedBy != null && !StringUtils.isEmpty(causedBy.getMessage())) {
        msg = causedBy.getMessage();
        causedBy = causedBy.getCause();
      }
      log.error("Failed to create - " + msg, e);
      throw (new ApplicationException(msg));
    }
  }

  /**
   * This will update common fields, e.g. 'modifiedAt', ... , and update the instance
   *
   * @return the updated instance
   */
  public T update() {
    preUpdate();
    if (!validate()) {
      return (null);
    }
    T obj = null;
    try {
      obj = realUpdate();
      postUpdate();

      return (obj);
    } catch (Exception e) {
      String msg = e.getMessage();
      Throwable causedBy = e.getCause();
      while (causedBy != null && !StringUtils.isEmpty(causedBy.getMessage())) {
        msg = causedBy.getMessage();
        causedBy = causedBy.getCause();
      }
      log.error("Failed to update - " + msg, e);
      throw (new ApplicationException(msg));
    }
  }


  public void delete() {
    if (!preDelete()) {
      return;
    }
    try {
      realDelete();
      postDelete();

    } catch (Exception e) {
      String msg = e.getMessage();
      Throwable causedBy = e.getCause();
      while (causedBy != null && !StringUtils.isEmpty(causedBy.getMessage())) {
        msg = causedBy.getMessage();
        causedBy = causedBy.getCause();
      }
      log.error("Failed to delete - " + msg, e);
      throw (new ApplicationException(msg));
    }
  }

  /**
   * This allow user to define additional validations
   *
   * @return true if passing all validations, otherwise false
   */
  protected boolean validate() {
    return (true);
  }

  /**
   * This will populate common fields, for creating an new instance
   */
  protected void preCreate() {
    try {
      if (hasId()) {
        Field declaredField = this.getClass().getDeclaredField(OBJ_ID);

        if (declaredField.getType().isAssignableFrom(Integer.class)) {
          // legacy Id, leave alone
        } else {
          // for now
          declaredField.set(this, UUID.randomUUID().toString());
        }
      }
    } catch (IllegalAccessException e) {
      LOGGER.error("Failed to access Id field", e);
    } catch (NoSuchFieldException e) {
      LOGGER.error("Not found Id field", e);
    }

    long currentTime = System.currentTimeMillis();
    setFieldValue(MODIFIED_AT, currentTime);
    setFieldValue(UPDATED_AT, currentTime);
    setFieldValue(CREATED_AT, currentTime);
    if (ActionContext.getUserId() != null) {
      setFieldValue(CREATED_BY,
              ActionContext.getUserId());  // ActionContext is populated by security filter
    }

    if (this.hasClientId() &&
            ActionContext.getClientId() != null) {
      setFieldValue(CLIENT_CID, ActionContext.getClientId());
    }
    sanitize(this);
    return;
  }

  /**
   * This will populate common fields, for updating an instance
   */
  protected void preUpdate() {
    setFieldValue(MODIFIED_AT, System.currentTimeMillis());
    if (ActionContext.getUserId() != null) {
      setFieldValue(MODIFIED_BY,
              ActionContext.getUserId());  // ActionContext is populated by security filter
    }
    sanitize(this);
  }

  /**
   *
   */
  protected boolean preDelete() {
    return (true);
  }

  private T realCreate() {
    Object obj = RepositoryUtil.getRepository(this.getClass()).save((T) this);
    return ((T) obj);
  }

  private T realUpdate() {
    return realCreate();
  }

  private void realDelete() {
    RepositoryUtil.getRepository(this.getClass()).delete((T) this);
  }

  protected void postCreate() {
    //
    // we try to generate the DbId based on the numerical Id value. If there's no numerical Id, skip
    //
    if (!hasId()) {
      return;
    }
    Field dbIdField = getDbIdField();
    if (dbIdField == null) {
      return;
    }
    try {
      String dbid = (String) dbIdField.get(this);
      if (DbId.isEmpty(dbid)) {
        Field idField = this.getClass().getDeclaredField(OBJ_ID);
        if (Integer.class.isAssignableFrom(idField.getType())) {
          Integer id = (Integer) idField.get(this);
          dbIdField.set(this, DbId.GenerateId(this, id).toString());
        }
        realUpdate();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to convert from object Id to DbId ", e);
    }
    return;
  }

  protected void postUpdate() {
    return;
  }

  protected void postDelete() {
    return;
  }

  private boolean setFieldValue(String name, Object val) {
    try {
      Field fld = this.getClass().getDeclaredField(name);
      if (fld.getType().isAssignableFrom(String.class) &&
              val != null) {
        // we have some columns that are 'int' in one table, while being 'string' in another
        fld.set(this, val.toString());
      } else {
        fld.set(this, val);
      }
      return (true);
    } catch (IllegalAccessException e) {
      LOGGER.debug("Failed to access common field {} ", name);
    } catch (NoSuchFieldException e) {
      LOGGER.debug("Not found common field {} ", name);
    }
    return (false);
  }
}
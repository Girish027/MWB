package com.tfs.learningsystems.db;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thymeleaf.util.StringUtils;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'tagging_guide_import_stats'. we will rename it to
 * 'tagging_guide_import_stat' (without the s )
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tagging_guide_import_stats")
public class TaggingGuideImportStatBO extends BusinessObject<TaggingGuideImportStatBO, String> {

  public static final String DB_PREFIX = "tgc";
  public static final String FLD_PROJECT_ID = "projectId";
  public static final String FLD_IMPORTED_AT = "importedAt";

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "cid")
  protected String cid = DbId.EMPTY.toString();

  @Column(name = "dbid")
  protected String dbid = DbId.EMPTY.toString();      // we will use this to replace the current ID

  @Column(name = "project_id")
  protected int projectId;

  @Column(name = "imported_by")
  protected String importedBy;

  @Column(name = "imported_at")
  protected Long importedAt;

  @Column(name = "valid_tag_count")
  protected int validTagCount;

  @Column(name = "invalid_tags")
  protected String invalidTags;

  @Column(name = "missing_tags")
  protected String missingTags;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  public void setInvalidTags(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      invalidTags = null;
    } else {
      invalidTags = String.join(", ", ids);
      ;
    }
  }

  public List<String> getInvalidTags() {
    if (StringUtils.isEmpty(this.invalidTags)) {
      return (new LinkedList<String>());
    }
    return (Arrays.asList(this.invalidTags.split(",")));
  }

  public void setMissingTags(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      missingTags = null;
    } else {
      missingTags = String.join(", ", ids);
      ;
    }
  }

  public List<String> getMissingTags() {
    if (StringUtils.isEmpty(this.missingTags)) {
      return (new LinkedList<String>());
    }
    return (Arrays.asList(this.missingTags.split(",")));
  }
}

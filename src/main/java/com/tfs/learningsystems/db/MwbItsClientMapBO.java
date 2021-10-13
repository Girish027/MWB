package com.tfs.learningsystems.db;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "mwb_its_client_map")
public class MwbItsClientMapBO extends BusinessObject<MwbItsClientMapBO, String> {

  public static final String DB_PREFIX = "mic";

  public static final String FLD_MWB_ID = "id";

  public static final String FLD_ITS_CLIENT_ID = "itsClientId";

  public static final String FLD_ITS_APP_ID = "itsAppId";

  public static final String FLD_ITS_ACCOUNT_ID = "itsAccountId";
  @Id
  @Column(name = "id")
  protected Integer id;
  @Column(name = "its_account_id")
  protected String itsAccountId;
  @Column(name = "its_client_id")
  protected String itsClientId;
  @Column(name = "its_app_id")
  protected String itsAppId;
  @Column(name = "description")
  protected String description;
  @Column(name = "created_at")
  protected Long createdAt;
  @Column(name = "modified_at")
  protected Long modifiedAt;
  @Column(name = "created_by")
  protected Integer createdBy;
  @Column(name = "modified_by")
  protected Integer modifiedBy;


  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }


}



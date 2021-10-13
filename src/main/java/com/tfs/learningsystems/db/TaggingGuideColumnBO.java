package com.tfs.learningsystems.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'tagging_guide_columns'. we will rename it to 'tagging_guide_column'
 * (without the s )
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tagging_guide_columns")
public class TaggingGuideColumnBO extends BusinessObject<TaggingGuideColumnBO, String> {

  public static final String DB_PREFIX = "tcs";
  public static final String FLD_NAME = "name";

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "dbid")
  protected String dbid = DbId.EMPTY.toString();      // we will use this to replace the current ID

  @Column(name = "name")
  protected String name;

  @Column(name = "required")
  protected Boolean required = false;

  @Column(name = "display_name")
  protected String displayName;


  @JsonIgnore
  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  @Override
  public boolean hasClientId() {
    return (false);
  }
}

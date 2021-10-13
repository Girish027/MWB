package com.tfs.learningsystems.db;

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
 * The table was named as 'file_column_mappings'. we will rename it to 'file_column_mapping'
 * (without the s )
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "file_column_mappings")
public class FileColumnMappingBO extends BusinessObject<FileColumnMappingBO, String> {

  public static final String DB_PREFIX = "fcm";

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "dbid")
  protected String dbid = DbId.EMPTY.toString();      // we will use this to replace the current ID

  @Column(name = "user_id")
  protected Integer userId;

  @Column(name = "column_id")
  protected Integer columnId;

  @Column(name = "column_index")
  protected Integer columnIndex;

  @Column(name = "display_name")
  protected String displayName;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  @Override
  public boolean hasClientId() {
    return (false);
  }
}

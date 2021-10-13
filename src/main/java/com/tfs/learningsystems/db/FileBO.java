package com.tfs.learningsystems.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The database operation of this object is handled by the BusinessObject. Hopefully, this will help
 * to simplify and unify these operations.
 *
 * The table was named as 'files'. we will rename it to 'file' (without the s )
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "files")
public class FileBO extends BusinessObject<FileBO, String> {

  public static final String DB_PREFIX = "fil";

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected Integer id;

  @Column(name = "file_id")
  protected String fileId;

  @Column(name = "name")
  protected String name;

  @Column(name = "system_name")
  protected String systemName;

  @Column(name = "user")
  protected String user;

  @Column(name = "created_at")
  protected Long createdAt;

  @Column(name = "modified_at")
  protected Long modifiedAt;

  //
  // no db column. But somehow used in <code>FilesApiServiceImpl</code>
  //
  @Transient
  private String uri;

  @Override
  public String getDbPrefix() {
    return DB_PREFIX;
  }

  @Override
  public boolean hasClientId() {
    return (false);
  }
}

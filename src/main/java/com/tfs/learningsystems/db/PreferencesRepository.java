package com.tfs.learningsystems.db;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Required by Spring Data. The access is hidden in BusinessObject and RepositoryUtil. Most
 * application development shouldn't have to know this, hopefully.
 */

@Component
public interface PreferencesRepository extends CrudRepository<PreferencesBO, String>,
    JpaSpecificationExecutor<PreferencesBO> {

}

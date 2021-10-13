package com.tfs.learningsystems.db;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface MetricsRepository extends CrudRepository<MetricsBO, String>,
        JpaSpecificationExecutor<MetricsBO> {
}

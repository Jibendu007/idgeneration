package com.volante.idgeneration.repository;

import com.volante.idgeneration.model.UniqueId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UniqueIdRepository extends MongoRepository<UniqueId, String> {
}

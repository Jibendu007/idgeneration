package com.volante.idgeneration.repository;

import com.volante.idgeneration.model.Payload;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PayloadRepository extends MongoRepository<Payload, String> {
}

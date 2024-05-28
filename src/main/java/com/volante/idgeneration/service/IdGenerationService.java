package com.volante.idgeneration.service;

import java.io.IOException;

public interface IdGenerationService {

    String generateId(String payloadJson) throws IOException;
}

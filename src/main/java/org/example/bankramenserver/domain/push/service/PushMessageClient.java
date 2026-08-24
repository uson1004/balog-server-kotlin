package org.example.bankramenserver.domain.push.service;

import java.util.Map;

public interface PushMessageClient {

    void send(String token, String title, String body, Map<String, String> data);
}

package org.example.bankramenserver.domain.push.service

interface PushMessageClient { fun send(token: String, title: String, body: String, data: Map<String, String>) }

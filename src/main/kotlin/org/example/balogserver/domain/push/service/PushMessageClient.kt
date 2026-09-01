package org.example.balogserver.domain.push.service

interface PushMessageClient { fun send(token: String, title: String, body: String, data: Map<String, String>) }

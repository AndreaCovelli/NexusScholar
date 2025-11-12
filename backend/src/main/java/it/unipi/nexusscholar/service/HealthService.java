package it.unipi.nexusscholar.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {

  public String getStatus() {
    return "Active";
  }
}

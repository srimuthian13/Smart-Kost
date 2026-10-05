package com.example.complaint_service.payload.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintReq {
    private Long roomId;
    private String title;
    private String description; 
}

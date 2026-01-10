package com.ctxh.volunteer.module.activity.service;

import com.ctxh.volunteer.common.util.AuthUtil;
import com.ctxh.volunteer.module.activity.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("activitySecurity")
public class ActivitySecurity {
    @Autowired
    private ActivityRepository repository;

    public boolean isOwner(Long activityId) {
        Long currentOrgId = AuthUtil.getIdFromAuthentication();
        // Kiểm tra xem Activity có tồn tại và thuộc về Organization này không
        return repository.existsByActivityIdAndOrganization_OrganizationId(activityId, currentOrgId);
    }
}
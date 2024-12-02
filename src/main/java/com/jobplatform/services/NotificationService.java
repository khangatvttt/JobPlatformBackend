package com.jobplatform.services;

import com.jobplatform.models.Notification;
import com.jobplatform.models.UserAccount;
import com.jobplatform.repositories.NotificationRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    public void addNotification(String message, String link, UserAccount user){
        Notification notification = new Notification();

        notification.setMessage(message);
        notification.setLink(link);
        notification.setUser(user);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);

    }

    @SneakyThrows
    public Page<Notification> getNotifications(Long userId, int page, int size){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!Objects.equals(userId, userAccount.getId())){
            throw new NoPermissionException();
        }
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserId(userId, pageable);
    }
}

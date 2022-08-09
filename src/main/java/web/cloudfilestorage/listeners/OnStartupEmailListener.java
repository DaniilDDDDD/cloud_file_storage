package web.cloudfilestorage.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import web.cloudfilestorage.utils.email.EmailService;


@Component
@Profile("prod")
public class OnStartupEmailListener implements ApplicationListener<ContextRefreshedEvent> {

    private final EmailService emailService;

    @Autowired
    public OnStartupEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Context refreshed!");
        emailService.sendSimpleMessage(
                "daniilpanyushin@yandex.ru",
                "Cloud File Storage",
                "Context refreshed!"
        );
    }
}

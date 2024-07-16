package gigabank.accountmanagement;

import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;

public class Main {
    public static void main(String[] args) {
        AnalyticsService analyticsService = new AnalyticsService();
        User user = new User();
        analyticsService.analyzePerformance(user);
    }
}
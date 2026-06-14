package com.mindful.wellness;

import com.mindful.wellness.entity.Appointment;
import com.mindful.wellness.entity.User;
import com.mindful.wellness.entity.Conversation;
import com.mindful.wellness.entity.Message;
import com.mindful.wellness.entity.CounsellorProfile;
import com.mindful.wellness.entity.StudentProfile;
import com.mindful.wellness.repository.AppointmentRepository;
import com.mindful.wellness.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DatabaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private com.mindful.wellness.repository.ConversationRepository conversationRepository;

    @Autowired
    private com.mindful.wellness.repository.MessageRepository messageRepository;

    @Autowired
    private com.mindful.wellness.repository.StudentProfileRepository studentProfileRepository;

    @Autowired
    private com.mindful.wellness.repository.CounsellorProfileRepository counsellorProfileRepository;

    @Autowired
    private com.mindful.wellness.repository.ChatSessionRepository chatSessionRepository;

    @Autowired
    private com.mindful.wellness.repository.ChatMessageRepository chatMessageRepository;

    @Autowired
    private com.mindful.wellness.repository.MentalHealthReportRepository mentalHealthReportRepository;

    @Test
    public void printDatabaseState() {
        System.out.println("=== PRINTING USERS ===");
        List<User> users = userRepository.findAll();
        for (User u : users) {
            System.out.println("User: ID=" + u.getId() + ", Email=" + u.getEmail() + ", Role=" + u.getRole() + ", Name=" + u.getFirstName() + " " + u.getLastName());
        }

        System.out.println("=== PRINTING APPOINTMENTS ===");
        List<Appointment> appts = appointmentRepository.findAll();
        for (Appointment a : appts) {
            System.out.println("Appt: ID=" + a.getId() + ", StudentId=" + a.getStudentId() + ", CounsellorId=" + a.getCounsellorId() + ", Status=" + a.getStatus() + ", Type=" + a.getAppointmentType() + ", Start=" + a.getScheduledStartTime());
        }

        System.out.println("=== PRINTING CONVERSATIONS ===");
        List<Conversation> convs = conversationRepository.findAll();
        for (Conversation c : convs) {
            System.out.println("Conv: ID=" + c.getId() + ", Part1=" + c.getParticipant1Id() + ", Part2=" + c.getParticipant2Id() + ", LastMsg=" + c.getLastMessagePreview());
        }

        System.out.println("=== PRINTING MESSAGES ===");
        List<Message> msgs = messageRepository.findAll();
        for (Message m : msgs) {
            System.out.println("Msg: ID=" + m.getId() + ", ConvID=" + m.getConversationId() + ", Sender=" + m.getSenderId() + ", Receiver=" + m.getReceiverId() + ", Content=" + m.getContent());
        }

        System.out.println("=== PRINTING COUNSELLOR PROFILES ===");
        List<CounsellorProfile> cp = counsellorProfileRepository.findAll();
        for (CounsellorProfile c : cp) {
            System.out.println("CounsellorProfile: UserID=" + c.getUserId() + ", Lic=" + c.getLicenseNumber() + ", Duration=" + c.getAppointmentDuration() + ", Accepting=" + c.getIsAcceptingNewStudents());
        }

        System.out.println("=== PRINTING STUDENT PROFILES ===");
        List<StudentProfile> sp = studentProfileRepository.findAll();
        for (StudentProfile s : sp) {
            System.out.println("StudentProfile: UserID=" + s.getUserId() + ", Consent=" + s.getConsentForDataSharing());
        }

        System.out.println("=== PRINTING CHAT SESSIONS ===");
        List<com.mindful.wellness.entity.ChatSession> sessions = chatSessionRepository.findAll();
        for (com.mindful.wellness.entity.ChatSession s : sessions) {
            System.out.println("ChatSession: ID=" + s.getId() + ", UserID=" + s.getUserId() + ", Type=" + s.getSessionType() + ", MsgCount=" + s.getMessageCount() + ", Active=" + s.getIsActive() + ", Severity=" + s.getDetectedSeverity());
        }

        System.out.println("=== PRINTING CHAT MESSAGES ===");
        List<com.mindful.wellness.entity.ChatMessage> chatMsgs = chatMessageRepository.findAll();
        for (com.mindful.wellness.entity.ChatMessage m : chatMsgs) {
            System.out.println("ChatMessage: ID=" + m.getId() + ", SessionID=" + m.getSessionId() + ", Role=" + m.getRole() + ", Content=" + m.getContent());
        }

        System.out.println("=== PRINTING MENTAL HEALTH REPORTS ===");
        List<com.mindful.wellness.entity.MentalHealthReport> reports = mentalHealthReportRepository.findAll();
        for (com.mindful.wellness.entity.MentalHealthReport r : reports) {
            System.out.println("Report: ID=" + r.getId() + ", UserID=" + r.getUserId() + ", Title=" + r.getTitle() + ", Level=" + r.getMentalStateLevel() + ", Score=" + r.getWellnessScore());
        }
    }
}

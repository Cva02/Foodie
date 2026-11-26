package com.foodie.admin.rider.service;

import com.foodie.admin.model.RiderSupportTicket;
import com.foodie.admin.repository.RiderSupportTicketRepository;
import com.foodie.admin.rider.dto.RiderSupportDtos.RiderSupportRequest;
import com.foodie.admin.rider.dto.RiderSupportDtos.RiderSupportView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderSupportService {

    private final RiderSupportTicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<RiderSupportView> getTickets(String riderId) {
        return ticketRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public RiderSupportView createTicket(
            String riderId,
            RiderSupportRequest request
    ) {
        if (request == null
                || request.subject() == null
                || request.message() == null
                || request.subject().isBlank()
                || request.message().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Subject and message are required."
            );
        }

        String subject = request.subject().trim();
        String message = request.message().trim();

        if (subject.length() > 120 || message.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Subject or message exceeds the allowed length."
            );
        }

        RiderSupportTicket ticket = new RiderSupportTicket();
        ticket.setRiderId(riderId);
        ticket.setSubject(subject);
        ticket.setMessage(message);

        return toView(ticketRepository.save(ticket));
    }

    private RiderSupportView toView(RiderSupportTicket ticket) {
        return new RiderSupportView(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getMessage(),
                ticket.getStatus(),
                ticket.getCreatedAt()
        );
    }
}
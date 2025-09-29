package io.scalebi.console.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.scalebi.console.entity.Ticket;
import io.scalebi.console.service.TicketService;

@Controller
public class TicketController {

  @Autowired
  private TicketService ticketService;

  @GetMapping("/tickets")
  public String getAll(
      Model model,
      @Param("keyword") String keyword,
      @Param("page") Integer page,
      @Param("size") Integer size,
      @Param("sort") String sort,
      @Param("dir") String dir) {
    try {
      int currentPage = (page == null || page < 1) ? 1 : page;
      int pageSize = (size == null || size < 1) ? 10 : size;
      // sorting
      String sortField = (sort == null || sort.isBlank()) ? "id" : sort;
      // whitelist of sortable fields
      java.util.Set<String> allowed = java.util.Set.of("id", "title", "description", "level", "published");
      if (!allowed.contains(sortField)) {
        sortField = "id";
      }
      Sort.Direction direction = (dir != null && dir.equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;

      Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(direction, sortField));

      Page<Ticket> ticketPage;
      if (keyword == null || keyword.trim().isEmpty()) {
        ticketPage = ticketService.findAll(pageable);
      } else {
        ticketPage = ticketService.findByTitleContainingIgnoreCase(keyword.trim(), pageable);
        model.addAttribute("keyword", keyword);
      }

      model.addAttribute("tickets", ticketPage.getContent());
      model.addAttribute("currentPage", currentPage);
      model.addAttribute("totalPages", ticketPage.getTotalPages());
      model.addAttribute("totalItems", ticketPage.getTotalElements());
      model.addAttribute("pageSize", pageSize);
      model.addAttribute("sort", sortField);
      model.addAttribute("dir", direction.isAscending() ? "asc" : "desc");
      model.addAttribute("reverseDir", direction.isAscending() ? "desc" : "asc");
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
    }

    return "tickets";
  }

  @GetMapping("/tickets/new")
  public String addTicket(Model model) {
    Ticket ticket = new Ticket();
    ticket.setPublished(true);

    model.addAttribute("ticket", ticket);
    model.addAttribute("pageTitle", "Create new Ticket");

    return "ticket_form";
  }

  @PostMapping("/tickets/save")
  public String saveTicket(Ticket ticket, RedirectAttributes redirectAttributes) {
    try {
      ticketService.save(ticket);

      redirectAttributes.addFlashAttribute("message", "The Ticket has been saved successfully!");
    } catch (Exception e) {
      redirectAttributes.addAttribute("message", e.getMessage());
    }

    return "redirect:/tickets";
  }

  @GetMapping("/tickets/{id}")
  public String editTicket(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Ticket ticket = ticketService.findById(id).get();

      model.addAttribute("ticket", ticket);
      model.addAttribute("pageTitle", "Edit Ticket (ID: " + id + ")");

      return "ticket_form";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());

      return "redirect:/tickets";
    }
  }

  @GetMapping("/tickets/delete/{id}")
  public String deleteTicket(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
      ticketService.deleteById(id);

      redirectAttributes.addFlashAttribute("message", "The Ticket with id=" + id + " has been deleted successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/tickets";
  }

  @GetMapping("/tickets/{id}/published/{status}")
  public String updateTicketPublishedStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean published,
      Model model, RedirectAttributes redirectAttributes) {
    try {
      ticketService.updatePublishedStatus(id, published);

      String status = published ? "published" : "disabled";
      String message = "The Ticket id=" + id + " has been " + status;

      redirectAttributes.addFlashAttribute("message", message);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/tickets";
  }
}

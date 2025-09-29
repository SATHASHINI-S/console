package io.scalebi.console.service;

import io.scalebi.console.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketService  {

    private final JdbcClient jdbcClient;

    public TicketService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Ticket> findByTitleContainingIgnoreCase(String keyword) {
        return jdbcClient.sql("SELECT * FROM tickets WHERE LOWER(title) LIKE LOWER(:keyword)")
                .param("keyword", "%" + keyword + "%")
                .query().listOfRows()
                .stream()
                .map(this::mapRow)
                .toList();
    }

    public Page<Ticket> findByTitleContainingIgnoreCase(String keyword, Pageable pageable) {
        String baseSql = "FROM tickets WHERE LOWER(title) LIKE LOWER(:keyword)";
        String orderBy = buildOrderBy(pageable);
        String sql = "SELECT * " + baseSql + orderBy + " LIMIT :limit OFFSET :offset";
        String countSql = "SELECT COUNT(*) " + baseSql;

        List<Ticket> content = jdbcClient.sql(sql)
                .param("keyword", "%" + keyword + "%")
                .param("limit", pageable.getPageSize())
                .param("offset", pageable.getOffset())
                .query()
                .listOfRows()
                .stream()
                .map(this::mapRow)
                .toList();

        long total = jdbcClient.sql(countSql)
                .param("keyword", "%" + keyword + "%")
                .query(Long.class)
                .single();

        return new PageImpl<>(content, pageable, total);
    }

    // Build a safe ORDER BY clause using Pageable sort and whitelisted columns
    private String buildOrderBy(Pageable pageable) {
        if (pageable == null || pageable.getSort() == null || pageable.getSort().isUnsorted()) {
            return " ORDER BY id DESC"; // default
        }

        // whitelist of valid columns
        Set<String> allowed = Set.of("id", "title", "description", "level", "published");

        StringBuilder sb = new StringBuilder();
        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            if (allowed.contains(property)) {
                if (sb.length() == 0) sb.append(" ORDER BY "); else sb.append(", ");
                sb.append(property).append(" ").append(order.isAscending() ? "ASC" : "DESC");
            }
        });

        if (sb.length() == 0) {
            return " ORDER BY id DESC";
        }
        return sb.toString();
    }

    public void updatePublishedStatus(Integer id, boolean published) {
        jdbcClient.sql("UPDATE tickets SET published = :published WHERE id = :id")
                .param("published", published)
                .param("id", id)
                .update();
    }

    public Page<Ticket> findAll(Pageable pageable) {
        String orderBy = buildOrderBy(pageable);
        String sql = "SELECT * FROM tickets" + orderBy + " LIMIT :limit OFFSET :offset";
        String countSql = "SELECT COUNT(*) FROM tickets";

        List<Ticket> content = jdbcClient.sql(sql)
                .param("limit", pageable.getPageSize())
                .param("offset", pageable.getOffset())
                .query()
                .listOfRows()
                .stream()
                .map(this::mapRow)
                .toList();

        long total = jdbcClient.sql(countSql).query(Long.class).single();

        return new PageImpl<>(content, pageable, total);
    }

   
    public <S extends Ticket> S save(S entity) {
        if (entity.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient.sql("""
                            INSERT INTO tickets(title, description, level, published)
                            VALUES(:title, :description, :level, :published) returning id
                            """)
                    .param("title", entity.getTitle())
                    .param("description", entity.getDescription())
                    .param("level", entity.getLevel())
                    .param("published", entity.isPublished())
                    .update(keyHolder);
            var id = keyHolder.getKeyAs(Integer.class);
            entity.setId(id);
        } else {
            jdbcClient.sql("""
                            UPDATE tickets 
                            SET title = :title, description = :description, level = :level, published = :published
                            WHERE id = :id
                            """)
                    .param("id", entity.getId())
                    .param("title", entity.getTitle())
                    .param("description", entity.getDescription())
                    .param("level", entity.getLevel())
                    .param("published", entity.isPublished())
                    .update();
        }
        return entity;
    }
   
    public Optional<Ticket> findById(Integer id) {
        return jdbcClient.sql("SELECT * FROM tickets WHERE id = :id")
                .param("id", id)
                .query()
                .listOfRows()
                .stream()
                .findFirst()
                .map(this::mapRow);
    }

   
    public boolean existsById(Integer id) {
        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM tickets WHERE id = :id")
                .param("id", id)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

   
    public void deleteById(Integer id) {
        jdbcClient.sql("DELETE FROM tickets WHERE id = :id")
                .param("id", id)
                .update();
    }


    // Row mapper for Ticket
    private Ticket mapRow(Map<String, Object> row) {
        Ticket ticket = new Ticket();
        ticket.setId((Integer) row.get("id"));
        ticket.setTitle((String) row.get("title"));
        ticket.setDescription((String) row.get("description"));
        ticket.setLevel((Integer) row.get("level"));
        ticket.setPublished((Boolean) row.get("published"));
        return ticket;
    }
}

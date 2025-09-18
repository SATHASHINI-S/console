package io.scalebi.console.repository;

import io.scalebi.console.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TicketService implements CrudRepository<Ticket, Integer> {

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
        String sql = "SELECT * " + baseSql + " LIMIT :limit OFFSET :offset";
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

    public void updatePublishedStatus(Integer id, boolean published) {
        jdbcClient.sql("UPDATE tickets SET published = :published WHERE id = :id")
                .param("published", published)
                .param("id", id)
                .update();
    }

    public Page<Ticket> findAll(Pageable pageable) {
        String sql = "SELECT * FROM tickets LIMIT :limit OFFSET :offset";
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

    @Override
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

    @Override
    public <S extends Ticket> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add(save(entity));
        }
        return saved;
    }

    @Override
    public Optional<Ticket> findById(Integer id) {
        return jdbcClient.sql("SELECT * FROM tickets WHERE id = :id")
                .param("id", id)
                .query()
                .listOfRows()
                .stream()
                .findFirst()
                .map(this::mapRow);
    }

    @Override
    public boolean existsById(Integer id) {
        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM tickets WHERE id = :id")
                .param("id", id)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public Iterable<Ticket> findAll() {
        return jdbcClient.sql("SELECT * FROM tickets")
                .query()
                .listOfRows()
                .stream()
                .map(this::mapRow)
                .toList();
    }

    @Override
    public Iterable<Ticket> findAllById(Iterable<Integer> ids) {
        String inClause = String.join(",", Collections.nCopies(((Collection<?>) ids).size(), "?"));
        return jdbcClient.sql("SELECT * FROM tickets WHERE id IN (" + inClause + ")")
                .params(ids)
                .query()
                .listOfRows()
                .stream()
                .map(this::mapRow)
                .toList();
    }

    @Override
    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM tickets")
                .query(Long.class)
                .single();
    }

    @Override
    public void deleteById(Integer id) {
        jdbcClient.sql("DELETE FROM tickets WHERE id = :id")
                .param("id", id)
                .update();
    }

    @Override
    public void delete(Ticket entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Ticket> entities) {
        for (Ticket ticket : entities) {
            delete(ticket);
        }
    }

    @Override
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM tickets").update();
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

package io.scalebi.console.repository;

import io.scalebi.console.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component
public class TicketRepository implements CrudRepository<Ticket, Integer> {

    private final DataSource dataSource;

    public TicketRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    List<Ticket> findByTitleContainingIgnoreCase(String keyword) {
        return null;
    }

    public Page<Ticket> findByTitleContainingIgnoreCase(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Ticket> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Ticket> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Ticket> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public Iterable<Ticket> findAll() {
        return null;
    }

    @Override
    public Iterable<Ticket> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void delete(Ticket entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {

    }

    @Override
    public void deleteAll(Iterable<? extends Ticket> entities) {

    }

    @Override
    public void deleteAll() {

    }

    public void updatePublishedStatus(Integer id, boolean published) {

    }

    public Page<Ticket> findAll(Pageable pageable) {
        return null;
    }
}

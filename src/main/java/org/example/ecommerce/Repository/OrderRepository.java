package org.example.ecommerce.Repository;

import jakarta.persistence.LockModeType;
import org.example.ecommerce.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    @Override
    Optional<Order> findById(Long aLong);

    /**
     * SELECT ... FOR UPDATE on the order, only if it belongs to the user: a concurrent checkout of the
     * same order waits, then sees the new status. Empty when missing or owned by someone else.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findOwnedByIdForUpdate(Long id, Long userId);
}

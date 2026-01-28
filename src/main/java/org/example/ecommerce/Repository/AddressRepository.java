package org.example.ecommerce.Repository;

import org.example.ecommerce.Model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Override
    Optional<Address> findById(Long id);

    @Override
    void deleteById(Long aLong);


}

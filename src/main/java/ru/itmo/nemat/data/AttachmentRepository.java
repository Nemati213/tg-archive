package ru.itmo.nemat.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.itmo.nemat.models.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
}

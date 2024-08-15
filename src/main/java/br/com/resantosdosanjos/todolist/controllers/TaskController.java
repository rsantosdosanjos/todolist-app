package br.com.resantosdosanjos.todolist.controllers;

import br.com.resantosdosanjos.todolist.models.TaskModel;
import br.com.resantosdosanjos.todolist.repositories.ITaskRepository;
import br.com.resantosdosanjos.todolist.utils.PropertyUtil;
import br.com.resantosdosanjos.todolist.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping()
    public ResponseEntity create(@RequestBody TaskModel model, HttpServletRequest request) {
        model.setUserId((UUID) request.getAttribute("userId"));

        var now = LocalDateTime.now();

        if (model.getStartAt().isBefore(now)) {
            return ResponseUtil.badRequest("The start date and time cannot be in the past.");
        }

        if (model.getEndAt() != null && model.getEndAt().isBefore(model.getStartAt())) {
            return ResponseUtil.badRequest("The end date and time cannot be before the start date and time.");
        }

        return ResponseUtil.created(taskRepository.save(model));
    }

    @GetMapping()
    public ResponseEntity list(HttpServletRequest request) {
        var tasks = taskRepository.findByUserId((UUID) request.getAttribute("userId"));
        return ResponseUtil.ok(tasks);
    }

    @PatchMapping("/{id}")
    public ResponseEntity update(@PathVariable UUID taskId, @RequestBody TaskModel model, HttpServletRequest request) {
        var task = taskRepository.findById(taskId)
                .orElse(null);

        if (task == null ) {
            return ResponseUtil.notFound("Task not found");
        }

        if (!task.getUserId().equals(request.getAttribute("userId"))) {
            return ResponseUtil.badRequest("Task does not belong to the user");
        }

        PropertyUtil.copyNonNullProperties(model, task);

        var updatedTask = taskRepository.save(task);
        return ResponseUtil.ok(updatedTask);
    }
}

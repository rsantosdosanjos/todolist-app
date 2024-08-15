package br.com.resantosdosanjos.todolist.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.resantosdosanjos.todolist.models.UserModel;
import br.com.resantosdosanjos.todolist.repositories.IUserRepository;
import br.com.resantosdosanjos.todolist.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping()
    public ResponseEntity create(@RequestBody UserModel model) {

        var user = this.userRepository.findByUsername(model.getUsername());

        if (user != null) {
            return ResponseUtil.badRequest("The user already exists");
        }

        var passwordHashred = BCrypt.withDefaults().hashToString(12, model.getPassword().toCharArray());

        model.setPassword(passwordHashred);

        return ResponseUtil.created(userRepository.save(model));
    }
}

package com.rentlytools.backend.core.category;

import com.rentlytools.backend.core.user.User;
import com.rentlytools.backend.core.user.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository repo;
    private final UserRepository userRepo;

    public CategoryController(CategoryRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @PostMapping("/add")
    public String add(
            @RequestParam Long adminId,
            @RequestParam String name,
            @RequestParam String icon
    ) {
        User u = userRepo.findById(adminId).orElse(null);
        if (u == null || !u.getRoleAdmin()) return "NOT ADMIN";

        Category c = new Category();
        c.setName(name);
        c.setIcon(icon);
        repo.save(c);

        return "Category added ✅";
    }

    @GetMapping("/all")
    public Object all() {
        return repo.findAll();
    }
}

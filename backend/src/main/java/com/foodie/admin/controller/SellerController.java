package com.foodie.admin.controller;

import com.foodie.admin.dto.SellerRequest;
import com.foodie.admin.dto.StatusNoteRequest;
import com.foodie.admin.model.Detail;
import com.foodie.admin.model.Seller;
import com.foodie.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final AdminService service;

    public SellerController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Seller> all() {
        return service.sellers();
    }

    @GetMapping("/{id}")
    public Detail<Seller> one(@PathVariable String id) {
        return service.seller(id);
    }

    @PatchMapping("/{id}/status")
    public Seller setStatus(@PathVariable String id, @RequestParam("value") String value,
                            @Valid @RequestBody(required = false) StatusNoteRequest body) {
        return service.setSellerStatus(id, value, body == null ? null : body.note());
    }

    /** Create: POST /api/sellers */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Seller create(@Valid @RequestBody SellerRequest body) {
        return service.createSeller(body);
    }

    /** Update: PUT /api/sellers/{id} */
    @PutMapping("/{id}")
    public Seller update(@PathVariable String id, @Valid @RequestBody SellerRequest body) {
        return service.updateSeller(id, body);
    }

    /** Delete: DELETE /api/sellers/{id} */
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable String id) {
        service.deleteSeller(id);
        return Map.of("message", "Seller deleted");
    }
}

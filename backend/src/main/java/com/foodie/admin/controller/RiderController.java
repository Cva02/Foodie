package com.foodie.admin.controller;

import com.foodie.admin.dto.RiderRequest;
import com.foodie.admin.dto.StatusNoteRequest;
import com.foodie.admin.model.Detail;
import com.foodie.admin.model.Rider;
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
@RequestMapping("/api/riders")
public class RiderController {

    private final AdminService service;

    public RiderController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rider> all() {
        return service.riders();
    }

    @GetMapping("/{id}")
    public Detail<Rider> one(@PathVariable String id) {
        return service.rider(id);
    }

    @PatchMapping("/{id}/status")
    public Rider setStatus(@PathVariable String id, @RequestParam("value") String value,
                           @Valid @RequestBody(required = false) StatusNoteRequest body) {
        return service.setRiderStatus(id, value, body == null ? null : body.note());
    }

    /** Create: POST /api/riders */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rider create(@Valid @RequestBody RiderRequest body) {
        return service.createRider(body);
    }

    /** Update: PUT /api/riders/{id} */
    @PutMapping("/{id}")
    public Rider update(@PathVariable String id, @Valid @RequestBody RiderRequest body) {
        return service.updateRider(id, body);
    }

    /** Delete: DELETE /api/riders/{id} */
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable String id) {
        service.deleteRider(id);
        return Map.of("message", "Rider deleted");
    }
}

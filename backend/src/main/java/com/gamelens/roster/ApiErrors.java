package com.gamelens.roster;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestControllerAdvice
public class ApiErrors {
 public record Error(String message, Map<String,String> fields) {}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<Error> validation(MethodArgumentNotValidException ex) {
  Map<String,String> fields=new LinkedHashMap<>();
  ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(),e.getDefaultMessage()));
  return ResponseEntity.badRequest().body(new Error("Please check the highlighted fields.",fields));
 }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
 ResponseEntity<Error> malformed(Exception ex) { return ResponseEntity.badRequest().body(new Error("Invalid request. Check names, jersey number, position, and handedness.",Map.of())); }
 @ExceptionHandler(ResponseStatusException.class)
 ResponseEntity<Error> missing(ResponseStatusException ex) { return ResponseEntity.status(ex.getStatusCode()).body(new Error(ex.getReason(),Map.of())); }
}

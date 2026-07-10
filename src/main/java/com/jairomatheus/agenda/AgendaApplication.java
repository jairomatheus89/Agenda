package com.jairomatheus.agenda;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class AgendaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendaApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigure(){
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry){
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:5173")
						.allowedMethods("*")
						.allowedHeaders("*");
			}
		};
	}
}

@Entity
class Contato {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String phone;
	private String email;

	public Integer getId(){
		return this.id;
	}
	public void setId(Integer id){
		this.id = id;
	}

	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}

	public String getPhone(){
		return this.phone;
	}
	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getEmail(){
		return this.email;
	}
	public void setEmail(String email){
		this.email = email;
	}
}

record ContatoDTO(
	Integer id,
	String name,
	String phone,
	String email
){}

@RestController
class BasicController {

	private final ContatoService service;

	public BasicController(ContatoService service){
		this.service = service;
	}

	@GetMapping("/")
	public List<Contato> getRootPage() {
		return service.htmlStringContacts();
	}

	@PostMapping("/add")
	public ResponseEntity add(@RequestBody ContatoDTO dto) {
		return service.addContact(dto);
	}

	@DeleteMapping("deletecontact")
	public ResponseEntity remove(@RequestBody ContatoDTO dto){
		return service.removeContact(dto);
	}
}

@Service
class ContatoService {

	private final ContatoRepository repository;

	public ContatoService(ContatoRepository repository){
		this.repository = repository;
	}

	public List<Contato> htmlStringContacts(){
		List<Contato> contacts = this.repository.findAll();
		return contacts;
	}

	public ResponseEntity addContact(ContatoDTO dto){
		Contato contact = new Contato();

		contact.setName(dto.name());
		contact.setPhone(dto.phone());
		contact.setEmail(dto.email());

		String msg;

		try{
			this.repository.save(contact);
			msg = "Contato Criado com Sucesso!";

			return ResponseEntity.ok(msg);
		} catch (DataIntegrityViolationException e) {

			String error = e.getMostSpecificCause().getMessage();
			if(error.contains("contato_phone_key")){
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Telefone repetido já existente");
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERRO AO SALVAR CONTATO");
		}
	}

	public ResponseEntity removeContact(ContatoDTO dto){
		String msg;

		Optional<Contato> user = this.repository.findById(dto.id());

		if(user.isEmpty()) return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario nao existente");

		Contato contato = user.get();

		String name = contato.getName();
		String phone = contato.getPhone();
		String email = contato.getEmail();

		try{
			this.repository.delete(contato);
			msg = "Usuário excluído com sucesso!";
			return ResponseEntity.ok(msg);
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMostSpecificCause().getMessage());
		}
	}
}

interface ContatoRepository extends JpaRepository<Contato, Integer> {}
package com.jairomatheus.agenda;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class AgendaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendaApplication.class, args);
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
	public String getRootPage() {
		return service.htmlStringContacts();
	}

	@PostMapping("/add")
	public String add(@RequestBody ContatoDTO dto) {
		return service.addContact(dto);
	}

	@DeleteMapping("deletecontact")
	public String remove(@RequestBody ContatoDTO dto){
		return service.removeContact(dto);
	}
}

@Service
class ContatoService {

	private final ContatoRepository repository;

	public ContatoService(ContatoRepository repository){
		this.repository = repository;
	}

	public String htmlStringContacts(){
		List<Contato> contacts = this.repository.findAll();

		StringBuilder contactsString = new StringBuilder();

		contactsString.append("<h1> LISTA DE CONTATOS </h1>");

		contacts.forEach(contato -> {
			contactsString.append(
				"Name: %s<br>Phone: %s<br>Email: %s<br>-----------------------------<br>"
					.formatted(
						contato.getName(),
						contato.getPhone(),
						contato.getEmail()
					)
			);
		});

		return """
            <body>
                <div>
                    %s
                </div>
            </body>
        """.formatted(contactsString);
	}

	public String addContact(ContatoDTO dto){
		Contato contact = new Contato();

		contact.setName(dto.name());
		contact.setPhone(dto.phone());
		contact.setEmail(dto.email());

		String msg;

		try{
			this.repository.save(contact);
			msg = """
                Olá %s\n
                PARECE QUE SEU NUMERO É %s\n
                E seu email: %s\n
            """.formatted(contact.getName(), contact.getPhone(), contact.getEmail());

			return msg;
		} catch (DataIntegrityViolationException e) {

			String error = e.getMostSpecificCause().getMessage();
			if(error.contains("contato_phone_key")){
				msg = "TELEFONE REPETIDO JA EXISTENTE...";
				return msg;
			}
			throw new ResponseStatusException(HttpStatus.CONFLICT, "INTEGRIDADE DE DADOS COMPROMETIDA...");
		}
	}

	public String removeContact(ContatoDTO dto){
		String msg;

		Optional<Contato> user = this.repository.findById(dto.id());

		if(user.isEmpty()) return "Nem Existe esse usuario mais brow!...";

		Contato contato = user.get();

		String name = contato.getName();
		String phone = contato.getPhone();
		String email = contato.getEmail();

		try{
			this.repository.delete(contato);
			msg = """
                USUARIO EXCLUIDO:\n
                Name: %s\n
                Phone: %s\n
                Email: %s\n
            """.formatted(name, phone, email);
			return msg;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMostSpecificCause().getMessage());
		}
	}
}

interface ContatoRepository extends JpaRepository<Contato, Integer> {}
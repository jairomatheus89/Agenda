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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

interface ContatoRepository extends JpaRepository<Contato, Integer> {}

class ContatoDTO {
	private String name;
	private String phone;
	private String email;

	public String getName(){
		return name;
	}
	public String getPhone(){
		return phone;
	}
	public String getEmail(){
		return email;
	}
}

@RestController
class BasicController {

	private ContatoRepository repository;

	public BasicController(ContatoRepository repository){
		this.repository = repository;
	}

	@GetMapping("/")
	public String getRootPage() {
		List<Contato> contacts = this.repository.findAll();

		StringBuilder contactsString = new StringBuilder();

		contactsString.append("<h1> LISTA DE CONTATOS </h1><br>");

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

	@PostMapping("/add")
	public String add(@RequestBody ContatoDTO entity) {

		Contato contact = new Contato();

		contact.setName(entity.getName());
		contact.setPhone(entity.getPhone());
		contact.setEmail(entity.getEmail());

		String msg;

		try{
			this.repository.save(contact);
			msg = """
                Olá %s\n
                PARECE QUE SEU NUMERO É %s\n
                E seu email: %s\n
            """.formatted(entity.getName(), entity.getPhone(), entity.getEmail());

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
}


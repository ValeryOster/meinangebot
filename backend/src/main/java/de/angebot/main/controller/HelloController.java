package de.angebot.main.controller;

import de.angebot.main.gathering.penny.PennyOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	@Autowired
	private PennyOffer pennyOffer;

	@RequestMapping("/home")
	public String index() {
		return "Gesammelt";
	}

}

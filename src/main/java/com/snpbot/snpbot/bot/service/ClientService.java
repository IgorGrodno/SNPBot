package com.snpbot.snpbot.bot.service;

import com.snpbot.snpbot.bot.model.Client;
import com.snpbot.snpbot.bot.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientService {
    @Autowired
    ClientRepository clientRepository;

    public void saveClient(Client client) {
        if (getClientByTelegrammUserId(client.getTelegrammUserId()).isEmpty()) {
            clientRepository.save(client);
        } else {
            Client clientToUpdate = getClientByTelegrammUserId(client.getTelegrammUserId()).get();
            clientToUpdate.setUtm_source(client.getUtm_source());
            clientToUpdate.setUtm_medium(client.getUtm_medium());
            clientToUpdate.setUtm_campaign(client.getUtm_campaign());
            clientToUpdate.setFirstname(client.getFirstname());
            clientToUpdate.setLastname(client.getLastname());
            clientToUpdate.setMiddlename(client.getMiddlename());
            clientToUpdate.setBirthDate(client.getBirthDate());
            clientToUpdate.setPathtofoto(client.getPathtofoto());
            clientToUpdate.setSex(client.isSex());
            clientRepository.save(clientToUpdate);
        }
    }


    public Optional<Client> getClientByTelegrammUserId(long id) {
        return clientRepository.findByTelegrammUserId(id);
    }


    public Client createClient(Map<String, String> clientParameters) {
        Client client = new Client();
        client.setTelegrammUserId(Long.valueOf(clientParameters.get("telegrammuserid")));
        if (clientParameters.containsKey("username")) {
            client.setUsername(clientParameters.get("username"));
        }
        client.setFirstname(clientParameters.get("firstname"));
        client.setLastname(clientParameters.get("lastname"));
        client.setMiddlename(clientParameters.get("middlename"));
        if (!clientParameters.containsKey("utm_source")) {
            client.setUtm_source(clientParameters.get("utm_source"));
        }
        if (!clientParameters.containsKey("utm_medium")) {
            client.setUtm_medium(clientParameters.get("utm_medium"));
        }
        if (!clientParameters.containsKey("utm_campaign")) {
            client.setUtm_campaign(clientParameters.get("utm_campaign"));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        client.setBirthDate(LocalDate.parse(clientParameters.get("birthDate"), formatter));
        client.setSex(Boolean.parseBoolean(clientParameters.get("sex")));
        if (clientParameters.containsKey("pathtofoto")) {
            client.setPathtofoto(clientParameters.get("pathtofoto"));
        }
        saveClient(client);
        return client;
    }
}

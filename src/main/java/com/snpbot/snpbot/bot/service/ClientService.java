package com.snpbot.snpbot.bot.service;

import com.snpbot.snpbot.bot.model.Client;
import com.snpbot.snpbot.bot.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {
    @Autowired
    ClientRepository clientRepository;

    public void saveClient(Client client) {
        if (getClient(client.getId()).isPresent()) {
            clientRepository.save(client);
        } else {
            Client clientToUpdate = getClient(client.getId()).get();
            clientToUpdate.setUtm_source(client.getUtm_source());
            clientToUpdate.setUtm_medium(client.getUtm_medium());
            clientToUpdate.setUtm_campaign(client.getUtm_campaign());
            clientToUpdate.setFirstname(client.getFirstname());
            clientToUpdate.setLastname(client.getLastname());
            clientToUpdate.setMiddleName(client.getMiddleName());
            clientToUpdate.setBirthDate(client.getBirthDate());
            clientToUpdate.setPathtofoto(client.getPathtofoto());
            clientRepository.save(clientToUpdate);
        }
    }

    public Optional<Client> getClient(long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> getClient(String username) {
        return clientRepository.findByUsername(username);
    }

    public Iterable<Client> getClients() {
        return clientRepository.findAll();
    }
}

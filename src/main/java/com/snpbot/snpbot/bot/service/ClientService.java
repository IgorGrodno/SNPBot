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

    public Optional<Client> getClient(long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> getClientByTelegrammUserId(long id) {
        return clientRepository.findByTelegrammUserId(id);
    }

    public Optional<Client> getClient(String username) {
        return clientRepository.findByUsername(username);
    }

    public Iterable<Client> getClients() {
        return clientRepository.findAll();
    }
}

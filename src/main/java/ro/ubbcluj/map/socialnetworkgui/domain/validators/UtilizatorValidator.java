package ro.ubbcluj.map.socialnetworkgui.domain.validators;

import ro.ubbcluj.map.socialnetworkgui.domain.Utilizator;

public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        //First name is empty
        if(entity.getFirstName().isEmpty()) {
            throw new ValidationException("First name can't be null!");
        }

        //Last name is empty
        if(entity.getLastName().isEmpty()) {
            throw new ValidationException("Last name can't be null!");
        }
    }
}


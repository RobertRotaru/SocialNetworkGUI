package ro.ubbcluj.map.socialnetworkgui.domain.validators;

import ro.ubbcluj.map.socialnetworkgui.domain.Prietenie;

public class PrietenieValidator implements Validator<Prietenie> {
    @Override
    public void validate(Prietenie entity) throws ValidationException {
        if(entity.getId().getLeft() == null || entity.getId().getRight() == null) {
            throw new ValidationException("IDs can't be null!");
        }
    }
}

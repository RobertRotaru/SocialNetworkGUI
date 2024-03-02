package ro.ubbcluj.map.socialnetworkgui.domain.validators;

import ro.ubbcluj.map.socialnetworkgui.domain.Message;

public class MessageValidator implements Validator<Message>{
    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getId() == null) {
            throw new ValidationException("ID can't be null!");
        }
    }
}

package ro.ubbcluj.map.socialnetworkgui.domain.validators;

import ro.ubbcluj.map.socialnetworkgui.domain.Request;

public class RequestValidator implements Validator<Request> {
    @Override
    public void validate(Request entity) throws ValidationException {
        if(entity.getFrom() == null || entity.getTo() == null) {
            throw new ValidationException("There must be two users in the request!");
        }
    }
}

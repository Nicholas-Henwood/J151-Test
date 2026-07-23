package za.ac.vzap.trytons.frontend.client.transfer;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TransferRequestValidator {
    public static boolean isValid(TransferRequest request){
        if(request == null){
            return false;
        }
        return (request.getTeamId()) != null &&
                (request.getRoundId()) != null &&
                (request.getRemovedPlayerId()) != null &&
                (request.getAddedPlayerId()) != null &&
                !request.getRemovedPlayerId().equals(request.getAddedPlayerId());

    }


}

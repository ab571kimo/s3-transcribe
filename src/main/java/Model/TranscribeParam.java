package Model;

import lombok.Data;

@Data
public class TranscribeParam {

    private boolean showSpeakerLabels;

    private int maxSpeakerLabels;

    private boolean channelIdentification;

    private boolean showAlternatives;

}

package com.example.magpie_wingman.data;

/**
 * Simplified version for US 02.05.02, acts as a connector to Stuart’s backend function
 * The actual random sampling & Firestore updates are already implemented in the backend
 */
public class LotteryFunction {

    /**
     * Calls the existing Firestore function to sample an amount of entrants from the event's waitlist&move them into the registrable subcollection.
     *
     * @param eventId       The Firestore event document ID
     * @param sampleSize    The number of entrants to sample
     * @param onSuccess     Callback when the sampling succeeds
     * @param onError       Callback when something fails
     */
    public static void runSampling(String eventId, int sampleSize, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        // Confirm if that's all input needed
        try {
            // TODO: Replace with the actual call of method name + class once confirmed w/ Stuart, import class if needed
            // Eg: EventRepository.createRegistrableUsers(eventId, sampleSize); (placeholder)

            // For now it simulates success until backend is ready
            System.out.println("Calling backend sampling for event: " + eventId + " with sample size: " + sampleSize);
            onSuccess.run();
        } catch (Exception e) {
            onError.accept(e);
        }
    }
}
# PhaseLogsVelocity
## Things to Check/Do
- Check that PhaseLogsBukkit is actually receiving the message from PhaseLogsVelocity and removing the message from it's cache (otherwise a memory leak would occur after a lot of messages in chat)
- Close the FileConfig after it is done being used to release the resources and it's good practice to close things that aren't being used anymore.

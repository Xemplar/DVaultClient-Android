# Denarius Vault
This is the official repo for the mobile Denarius wallet (D-Vault). You will be able to do may things here as you can do on the QT wallet. Only thing is that many people will be sharing one node, DVServer for your standard wallet and DVStaker for staking. Features you can expect:
- Multiple addresses that you can use for whatever
- Ability to stake right from your phone
- Proof of Data
- Have the option to keep priv keys on your phone rather than in the node
- Password/Fingerprint protection for payments, staking, and access to the app
- Address book and messages
- Block explorer

## Current progress
We have the following currently working:
- Server communicates with client
- Client can send commands to server such as `getinfo` and `getblockheight`
- Client can securely communicate with server for account creation and login
- Client can send TXs, request addresses, see balances per address and total
- Server remembers clients and balances using SQLite
- Server tracks incoming TXs
- Server tracks stakes
- Server deducts balances for out TXs

## Next Steps
The following are what we are working on right now
- Local key transaction signing
- Mobile App
- Tribus Algo for POD

## To Do
- Block explorer
- *more to come*

## Rules for git workflow
+ **main** is the main branch of the project. 
At the end of each sprint all the work from the **develop** branch goes to **main**. Only the tech lead can make changes to the **main** branch

+ **develop** is the branch that stores working versions with changes during each sprint. Only the tech lead has the right to push directly to **develop**.
Most of the changes are done through the merge operation of the **feature_(name)** branches by the tech lead.

+ **feature_(name)** (For example: feature_update_docs) is the branch for adding specific feature. Everyone must push they changes to such branches and make pull requests to **develop** branch, for reviewing by tech lead. Only exception is docs changes, they are reviewed by @mashaverdina.

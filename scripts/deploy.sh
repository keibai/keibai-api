# Deployment to Heroku

if [ "$TRAVIS_BRANCH" == "master" -a "$TRAVIS_PULL_REQUEST" == "false" ]; then
    openssl aes-256-cbc -K $encrypted_44a58ea948c2_key -iv $encrypted_44a58ea948c2_iv -in context.xml.enc -out src/main/webapp/META-INF/context.xml -d
    mvn heroku:deploy
fi

tailwind:
	tailwindcss -i ./src/main/resources/static/styles/index.css -o ./src/main/resources/static/styles/output.css
deploy:
	./eb-config.sh && eb deploy
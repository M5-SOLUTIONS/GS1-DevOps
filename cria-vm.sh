#!/bin/bash

# ==========================================
# VARIÁVEIS DE CONFIGURAÇÃO
# ==========================================
RESOURCE_GROUP="rg-m5"
LOCATION="canadacentral"
VM_NAME="vm-m5"
IMAGE="almalinux:almalinux-x86_64:10-gen2:10.1.202605180"
SIZE="Standard_B2als_v2"

ADMIN_USERNAME="admlnx"
ADMIN_PASSWORD="Fiap@2tdsvms"
DISK_SKU="StandardSSD_LRS"
PORT=22 # Alterado para 22 (SSH), já que a VM é Linux
SHUTDOWN_TIME="0230" # 23:30h horário de Brasília (UTC-3)

# ==========================================
# 1. CRIAÇÃO DA INFRAESTRUTURA
# ==========================================
echo "Criando grupo de recursos: $RESOURCE_GROUP..."
az group create --name $RESOURCE_GROUP --location $LOCATION

echo "Aceitando os Termos Legais da Imagem..."
az vm image terms accept --urn $IMAGE

echo "Criando a máquina virtual: $VM_NAME..."
az vm create \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --image $IMAGE \
  --size $SIZE \
  --authentication-type password \
  --admin-username $ADMIN_USERNAME \
  --admin-password $ADMIN_PASSWORD \
  --storage-sku $DISK_SKU \
  --public-ip-sku Standard

echo "Abrindo porta $PORT para SSH..."
az vm open-port --port $PORT --resource-group $RESOURCE_GROUP --name $VM_NAME

echo "Abrindo porta 8080 para o projeto..."
az vm open-port --port 8080 --resource-group $RESOURCE_GROUP --name $VM_NAME --priority 910

# ==========================================
# 2. INSTALAÇÃO DE FERRAMENTAS (DENTRO DA VM)
# ==========================================
echo "Iniciando a instalação de Ferramentas..."

echo "Instalando tree, git, nano e utilitários..."
az vm run-command invoke \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --command-id RunShellScript \
  --scripts "
    sudo yum update -y
    sudo yum install -y tree git nano yum-utils
  "

echo "Instalando Azure CLI..."
az vm run-command invoke \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --command-id RunShellScript \
  --scripts "
    sudo rpm --import https://packages.microsoft.com/keys/microsoft.asc
    sudo dnf install -y https://packages.microsoft.com/config/rhel/9/packages-microsoft-prod.rpm
    sudo dnf install -y azure-cli
  "

echo "Instalando Docker..."
az vm run-command invoke \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --command-id RunShellScript \
  --scripts "
    sudo yum-config-manager --add-repo https://download.docker.com/linux/rhel/docker-ce.repo
    sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  "

echo "Configurando permissões do Docker..."
az vm run-command invoke \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --command-id RunShellScript \
  --scripts "
    sudo systemctl start docker
    sudo systemctl enable docker.service
    sudo systemctl enable containerd.service
    sudo usermod -aG docker $ADMIN_USERNAME
  "

# ==========================================
# 3. CAPTURA DO IP E FINALIZAÇÃO
# ==========================================
echo "Obtendo IP público da VM..."
# Correção: Busca o IP diretamente associado à placa de rede da VM criada
PUBLIC_IP=$(az vm list-ip-addresses \
  --resource-group $RESOURCE_GROUP \
  --name $VM_NAME \
  --query "[0].virtualMachine.network.publicIpAddresses[0].ipAddress" \
  --output tsv)

echo ""
echo "============================="
echo "VM CONFIGURADA COM SUCESSO!"
echo "============================="
echo ""
echo "Softwares instalados:"
echo "- Git"
echo "- Nano"
echo "- Azure CLI"
echo "- Docker (configurado)"
echo ""
echo "Para conectar via SSH execute:"
echo "ssh $ADMIN_USERNAME@$PUBLIC_IP"
echo ""
echo "Senha: $ADMIN_PASSWORD"
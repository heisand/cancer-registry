import {
    Box,
    Button,
    Heading,
    Spinner,
    TableContainer,
    Table,
    Thead,
    Tr,
    Th,
    Tbody,
    Td,
    FormControl,
    FormLabel,
    Input,
    Modal,
    ModalBody,
    ModalCloseButton,
    ModalContent,
    ModalFooter,
    ModalHeader,
    ModalOverlay,
    useDisclosure,
  } from "@chakra-ui/react";
  import { getUserRoles } from "../api/api";
  import { useRef, useState } from "react";
  import { UserRole } from "../types/userrole";
  
  export function UserRolesByUserId() {
    const { isOpen, onOpen, onClose } = useDisclosure();
    const initialRef = useRef(null);
    const [userId, setUserId] = useState("");
    const [userRoles, setUserRoles] = useState<UserRole[]>([]);
    const [loading, setLoading] = useState(true);
  
    const handleUserIdChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      setUserId(e.target.value);
    };
  
    function handleGetUserRoles() {
      if (userId !== "") {
        fetchUserRoles();
      }
    }
  
    async function fetchUserRoles() {
      try {
        const response = await getUserRoles(userId)
        setUserRoles(response);
      } catch (error) {
        console.error("Error fetching user roles:", error);
      } finally {
        setLoading(false);
      }
    }
  
    return (
      <Box>
        <Button colorScheme="brand" size="lg" color="#393c61" onClick={onOpen}>
          Get user roles by user ID
        </Button>
        <Modal isOpen={isOpen} onClose={onClose} isCentered>
          <ModalOverlay />
          <ModalContent>
            <ModalHeader>Which user ID?</ModalHeader>
            <ModalCloseButton />
            <ModalBody pb={6}>
              <FormControl isRequired marginTop="24px">
                <FormLabel>User ID</FormLabel>
                <Input
                  ref={initialRef}
                  placeholder="User ID"
                  type="number"
                  onChange={handleUserIdChange}
                />
              </FormControl>
            </ModalBody>
            <ModalFooter>
              <Button
                colorScheme="brand"
                mr={3}
                color="#393c61"
                onClick={handleGetUserRoles}
              >
                OK
              </Button>
              <Button onClick={onClose}>Cancel</Button>
            </ModalFooter>
          </ModalContent>
        </Modal>
        {userRoles.length > 0 ? (
          <Box marginTop="48px">
            <Heading>Valid user roles</Heading>
            {loading ? (
              <Spinner size="xl" />
            ) : (
              <TableContainer>
                <Table colorScheme="brand">
                  <Thead>
                    <Tr>
                      <Th>ID</Th>
                      <Th>Version</Th>
                      <Th>UserId</Th>
                      <Th>UnitID</Th>
                      <Th>RoleId</Th>
                      <Th>ValidFrom</Th>
                      <Th>ValidTo</Th>
                    </Tr>
                  </Thead>
                  <Tbody>
                    {userRoles.map((userRole) => (
                      <Tr key={userRole.id}>
                        <Td>{userRole.id}</Td>
                        <Td>{userRole.version}</Td>
                        <Td>{userRole.userId}</Td>
                        <Td>{userRole.unitId}</Td>
                        <Td>{userRole.roleId}</Td>
                        <Td>{userRole.validFrom}</Td>
                        <Td>{userRole.validTo}</Td>
                      </Tr>
                    ))}
                  </Tbody>
                </Table>
              </TableContainer>
            )}
          </Box>
        ) : null}
      </Box>
    );
  }
  